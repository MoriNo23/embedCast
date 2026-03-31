using System.Net.WebSockets;
using System.Text;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.UseDefaultFiles();
app.UseStaticFiles();
app.UseWebSockets();

ClientWebSocket? tvWebSocket = null;
WebSocket? browserWebSocket = null;

app.MapPost("/api/connect", async (HttpContext context) =>
{
    var payload = await context.Request.ReadFromJsonAsync<Dictionary<string, string>>();
    if (payload != null && payload.TryGetValue("ip", out var ip))
    {
        if (tvWebSocket?.State == WebSocketState.Open) 
            await tvWebSocket.CloseAsync(WebSocketCloseStatus.NormalClosure, "Reconnecting", CancellationToken.None);
            
        tvWebSocket = new ClientWebSocket();
        try
        {
            await tvWebSocket.ConnectAsync(new Uri($"ws://{ip}:8080"), CancellationToken.None);
            _ = ListenToTv(); // Start background listener
            return Results.Ok(new { status = "connected", ip });
        }
        catch (Exception ex)
        {
            tvWebSocket = null;
            return Results.BadRequest(new { error = ex.Message });
        }
    }
    return Results.BadRequest("Invalid payload");
});

app.Map("/ws", async (context) =>
{
    if (context.WebSockets.IsWebSocketRequest)
    {
        browserWebSocket = await context.WebSockets.AcceptWebSocketAsync();
        await ListenToBrowser();
    }
    else
    {
        context.Response.StatusCode = 400;
    }
});

app.MapPost("/api/command", async (HttpContext context) =>
{
    if (tvWebSocket?.State != WebSocketState.Open) return Results.BadRequest("Not connected to TV");
    
    using var reader = new StreamReader(context.Request.Body);
    var body = await reader.ReadToEndAsync();
    await SendToTv(body);
    return Results.Ok(new { status = "sent" });
});

async Task ListenToTv()
{
    var buffer = new byte[1024 * 4];
    while (tvWebSocket?.State == WebSocketState.Open)
    {
        var result = await tvWebSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        if (result.MessageType == WebSocketMessageType.Close) break;
        
        var message = Encoding.UTF8.GetString(buffer, 0, result.Count);
        if (browserWebSocket?.State == WebSocketState.Open)
        {
            await browserWebSocket.SendAsync(new ArraySegment<byte>(buffer, 0, result.Count), WebSocketMessageType.Text, true, CancellationToken.None);
        }
    }
}

async Task ListenToBrowser()
{
    var buffer = new byte[1024 * 4];
    while (browserWebSocket?.State == WebSocketState.Open)
    {
        var result = await browserWebSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        if (result.MessageType == WebSocketMessageType.Close) break;
        
        var message = Encoding.UTF8.GetString(buffer, 0, result.Count);
        await SendToTv(message);
    }
}

async Task SendToTv(string message)
{
    if (tvWebSocket?.State == WebSocketState.Open)
    {
        var bytes = Encoding.UTF8.GetBytes(message);
        await tvWebSocket.SendAsync(new ArraySegment<byte>(bytes), WebSocketMessageType.Text, true, CancellationToken.None);
    }
}

app.Run("http://localhost:3000");