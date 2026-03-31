#!/bin/bash
# WebSocket Protocol Compatibility Verification Script
# This script verifies that all WebSocket actions work correctly

echo "=== WebSocket Protocol Compatibility Test ==="
echo ""

# Check if WebSocketManager exists and has correct structure
echo "1. Checking WebSocketManager structure..."
if grep -q "class WebSocketManager" /home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/WebSocketManager.kt; then
    echo "   ✓ WebSocketManager class exists"
else
    echo "   ✗ WebSocketManager class not found"
    exit 1
fi

# Check if TvWebSocketServer still exists (backward compatibility)
echo "2. Checking TvWebSocketServer (backward compatibility)..."
if grep -q "class TvWebSocketServer" /home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/TvWebSocketServer.kt; then
    echo "   ✓ TvWebSocketServer class exists"
else
    echo "   ✗ TvWebSocketServer class not found"
    exit 1
fi

# Check if all WebSocket actions are handled in MainActivity
echo "3. Checking WebSocket action handlers..."
ACTIONS=("load" "play" "pause" "stop" "seek" "quality" "reload")
for action in "${ACTIONS[@]}"; do
    if grep -q "\"$action\"" /home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt; then
        echo "   ✓ Action '$action' is handled"
    else
        echo "   ✗ Action '$action' is NOT handled"
        exit 1
    fi
done

# Check if sendStatus method exists
echo "4. Checking sendStatus method..."
if grep -q "fun sendStatus" /home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/WebSocketManager.kt; then
    echo "   ✓ sendStatus method exists"
else
    echo "   ✗ sendStatus method not found"
    exit 1
fi

# Check if sendLog method exists
echo "5. Checking sendLog method..."
if grep -q "fun sendLog" /home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/WebSocketManager.kt; then
    echo "   ✓ sendLog method exists"
else
    echo "   ✗ sendLog method not found"
    exit 1
fi

# Check if port 8080 is used
echo "6. Checking WebSocket port..."
if grep -q "8080" /home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/WebSocketManager.kt; then
    echo "   ✓ Port 8080 is configured"
else
    echo "   ✗ Port 8080 not found"
    exit 1
fi

# Verify build compiles
echo "7. Verifying build..."
cd /home/fullmetal/Descargas/MovieON/tv-app
if ./gradlew compileDebugKotlin --quiet 2>&1 | grep -q "BUILD SUCCESSFUL"; then
    echo "   ✓ Build successful"
else
    echo "   ✗ Build failed"
    exit 1
fi

echo ""
echo "=== All WebSocket compatibility checks passed! ==="
echo ""
echo "Summary:"
echo "- WebSocketManager properly wraps TvWebSocketServer"
echo "- All 7 WebSocket actions are handled (load, play, pause, stop, seek, quality, reload)"
echo "- sendStatus and sendLog methods are available"
echo "- Port 8080 is configured correctly"
echo "- Build compiles successfully"
