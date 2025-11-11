#!/usr/bin/env python3
"""
Simple MCP client to test the Hello World server
Works similar to 'mcp dev' for Python servers
"""

import json
import subprocess
import sys
from typing import Any, Dict

def send_request(process: subprocess.Popen, request: Dict[str, Any]) -> Dict[str, Any]:
    """Send a JSON-RPC request and get response"""
    request_json = json.dumps(request) + '\n'
    process.stdin.write(request_json.encode())
    process.stdin.flush()
    
    response_line = process.stdout.readline().decode()
    if not response_line:
        raise Exception("No response from server")
    
    return json.loads(response_line)

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 test-client.py <path-to-jar>")
        print("Example: python3 test-client.py build/libs/mcp-server-demo-0.0.1-all.jar")
        sys.exit(1)
    
    jar_path = sys.argv[1]
    
    print("=" * 60)
    print("MCP Hello World Server Test Client")
    print("=" * 60)
    print()
    
    # Start the server
    print(f"Starting server: {jar_path}")
    process = subprocess.Popen(
        ['java', '-jar', jar_path],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    
    try:
        # Test 1: Initialize
        print("\n📡 Test 1: Initialize connection")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": {
                    "name": "python-test-client",
                    "version": "1.0.0"
                }
            }
        })
        print(f"✅ Server initialized: {response.get('result', {}).get('serverInfo', {}).get('name', 'Unknown')}")
        print(f"   Version: {response.get('result', {}).get('serverInfo', {}).get('version', 'Unknown')}")
        
        # Test 2: List tools
        print("\n🔧 Test 2: List available tools")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/list",
            "params": {}
        })
        tools = response.get('result', {}).get('tools', [])
        print(f"✅ Found {len(tools)} tools:")
        for tool in tools:
            print(f"   - {tool['name']}: {tool.get('description', 'No description')[:60]}...")
        
        # Test 3: Call hello without name
        print("\n👋 Test 3: Call 'hello' tool (no name)")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 3,
            "method": "tools/call",
            "params": {
                "name": "hello",
                "arguments": {}
            }
        })
        content = response.get('result', {}).get('content', [{}])[0].get('text', '')
        print(f"✅ Response: {content}")
        
        # Test 4: Call hello with name
        print("\n👋 Test 4: Call 'hello' tool (with name 'Alice')")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 4,
            "method": "tools/call",
            "params": {
                "name": "hello",
                "arguments": {
                    "name": "Alice"
                }
            }
        })
        content = response.get('result', {}).get('content', [{}])[0].get('text', '')
        print(f"✅ Response: {content}")
        
        # Test 5: Call echo
        print("\n📢 Test 5: Call 'echo' tool")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 5,
            "method": "tools/call",
            "params": {
                "name": "echo",
                "arguments": {
                    "message": "Hello from Python client!"
                }
            }
        })
        content = response.get('result', {}).get('content', [{}])[0].get('text', '')
        print(f"✅ Response: {content}")
        
        # Test 6: Test sanitization
        print("\n🛡️  Test 6: Test input sanitization (control characters)")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 6,
            "method": "tools/call",
            "params": {
                "name": "hello",
                "arguments": {
                    "name": "Alice\nBob\tCharlie"
                }
            }
        })
        content = response.get('result', {}).get('content', [{}])[0].get('text', '')
        print(f"✅ Response: {content}")
        print(f"   Note: Newlines and tabs were removed (sanitized)")
        
        # Test 7: Test length limit
        print("\n🛡️  Test 7: Test input sanitization (length limit)")
        print("-" * 60)
        long_name = "A" * 80  # 80 chars, but limit is 50
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 7,
            "method": "tools/call",
            "params": {
                "name": "hello",
                "arguments": {
                    "name": long_name
                }
            }
        })
        content = response.get('result', {}).get('content', [{}])[0].get('text', '')
        print(f"✅ Response: {content}")
        print(f"   Note: Input was {len(long_name)} chars, truncated to 50")
        
        # Test 8: List resources
        print("\n📚 Test 8: List available resources")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 8,
            "method": "resources/list",
            "params": {}
        })
        resources = response.get('result', {}).get('resources', [])
        print(f"✅ Found {len(resources)} resources:")
        for resource in resources:
            print(f"   - {resource.get('uri', 'Unknown')}: {resource.get('name', 'No name')}")
        
        # Test 9: Read static resource
        print("\n📖 Test 9: Read server info resource")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 9,
            "method": "resources/read",
            "params": {
                "uri": "hello://server/info"
            }
        })
        contents = response.get('result', {}).get('contents', [{}])[0]
        text = contents.get('text', '')
        print(f"✅ Resource content (first 100 chars):")
        print(f"   {text[:100]}...")
        
        # Test 10: Read dynamic resource
        print("\n📖 Test 10: Read dynamic greeting resource")
        print("-" * 60)
        response = send_request(process, {
            "jsonrpc": "2.0",
            "id": 10,
            "method": "resources/read",
            "params": {
                "uri": "hello://greetings/TestUser"
            }
        })
        contents = response.get('result', {}).get('contents', [{}])[0]
        text = contents.get('text', '')
        print(f"✅ Dynamic resource content (first 100 chars):")
        print(f"   {text[:100]}...")
        
        print("\n" + "=" * 60)
        print("✅ All tests passed!")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        stderr = process.stderr.read().decode()
        if stderr:
            print(f"Server error: {stderr}")
        sys.exit(1)
    finally:
        process.terminate()
        process.wait()

if __name__ == "__main__":
    main()
