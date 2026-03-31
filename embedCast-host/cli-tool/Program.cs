using System;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using System.Collections.Generic;

class Program
{
    static readonly HttpClient client = new HttpClient { BaseAddress = new Uri("http://localhost:3000/") };

    static async Task Main(string[] args)
    {
        if (args.Length == 0)
        {
            ShowHelp();
            return;
        }

        string command = args[0].ToLower();

        switch (command)
        {
            case "connect":
                if (args.Length < 2) { Console.WriteLine("Usage: connect <ip>"); return; }
                await ConnectToTv(args[1]);
                break;
            
            case "load":
                if (args.Length < 2) { Console.WriteLine("Usage: load <url>"); return; }
                await SendCommand("load", new { url = args[1] });
                break;
            
            case "play":
                await SendCommand("play", new { });
                break;

            case "pause":
                await SendCommand("pause", new { });
                break;

            case "stop":
                await SendCommand("stop", new { });
                break;

            case "reload":
                await SendCommand("reload", new { });
                break;

            default:
                Console.WriteLine($"Unknown command: {command}");
                ShowHelp();
                break;
        }
    }

    static async Task ConnectToTv(string ip)
    {
        var json = JsonSerializer.Serialize(new { ip });
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        
        try
        {
            var res = await client.PostAsync("api/connect", content);
            Console.WriteLine(await res.Content.ReadAsStringAsync());
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Connection failed: {ex.Message}. Make sure the Web GUI Server is running.");
        }
    }

    static async Task SendCommand(string action, object payloadData)
    {
        var jsonString = JsonSerializer.Serialize(payloadData);
        var dict = JsonSerializer.Deserialize<Dictionary<string, object>>(jsonString) ?? new Dictionary<string, object>();
        dict["action"] = action;
        
        var json = JsonSerializer.Serialize(dict);
        var content = new StringContent(json, Encoding.UTF8, "application/json");

        try
        {
            var res = await client.PostAsync("api/command", content);
            Console.WriteLine(await res.Content.ReadAsStringAsync());
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Command failed: {ex.Message}");
        }
    }

    static void ShowHelp()
    {
        Console.WriteLine("EmbedCast CLI - Remote Control Tool");
        Console.WriteLine("Commands:");
        Console.WriteLine("  connect <ip>    Connect to Android TV WebSocket");
        Console.WriteLine("  load <url>      Load video embed URL on TV");
        Console.WriteLine("  stop            Stop video and return to standby");
        Console.WriteLine("  reload          Force reload current video link");
    }
}