# WebSocket Protocol - EmbedCast TV

## Overview
This document describes the WebSocket protocol used by EmbedCast TV (formerly MovieON TV) for remote control communication.

## Connection
- **Host**: TV's IP address on port 8080
- **Format**: `ws://<TV_IP>:8080`
- **Protocol**: JSON messages

## Client → TV Commands

### Message Format
```json
{
  "action": "<action_name>",
  "url": "<video_url>",      // optional, for "load" action
  "seconds": <int>,           // optional, for "seek" action
  "value": "<string>"         // optional, for "quality" action
}
```

### Supported Actions

| Action | Required Fields | Description | Example |
|--------|----------------|-------------|---------|
| `load` | `url` | Load a video URL in the WebView | `{"action": "load", "url": "https://example.com/embed/123"}` |
| `play` | - | Toggle play/pause | `{"action": "play"}` |
| `pause` | - | Pause the video | `{"action": "pause"}` |
| `stop` | - | Stop video and show guide | `{"action": "stop"}` |
| `seek` | `seconds` | Seek relative seconds (+/-) | `{"action": "seek", "seconds": 10}` |
| `quality` | `value` | Set quality (1-9) | `{"action": "quality", "value": "2"}` |
| `reload` | - | Force reload (clear cache, new token) | `{"action": "reload"}` |

## TV → Client Status Messages

### Status Format
```json
{
  "type": "status",
  "currentTime": <float>,
  "duration": <float>,
  "paused": <boolean>
}
```

### Log Format
```json
{
  "type": "log",
  "message": "<log_message>"
}
```

## Usage Examples

### Load a video
```json
{"action": "load", "url": "https://stream.example.com/embed/abc123"}
```

### Play/Pause
```json
{"action": "play"}
```

### Seek forward 10 seconds
```json
{"action": "seek", "seconds": 10}
```

### Seek backward 10 seconds
```json
{"action": "seek", "seconds": -10}
```

### Set quality (1 = highest, 9 = lowest)
```json
{"action": "quality", "value": "1"}
```

### Force reload (for token refresh)
```json
{"action": "reload"}
```

### Stop and show guide
```json
{"action": "stop"}
```

## Notes
- The WebSocket server runs on port 8080
- Status updates are broadcast to all connected clients
- The server supports multiple simultaneous connections
- The protocol is designed for backward compatibility
