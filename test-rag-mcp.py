#!/usr/bin/env python3
"""
Test script for the RAG-powered MCP server
"""

import asyncio
import json
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client

async def main():
    server_params = StdioServerParameters(
        command="./gradlew",
        args=["--console=plain", "-q", "run"],
        env=None
    )
    
    async with stdio_client(server_params) as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()
            
            print("=" * 80)
            print("Testing RAG-powered MCP Server")
            print("=" * 80)
            print()
            
            # List available tools
            print("Available tools:")
            tools = await session.list_tools()
            for tool in tools.tools:
                print(f"  - {tool.name}: {tool.description}")
            print()
            
            # Test RAG query
            print("=" * 80)
            print("Testing RAG Query Tool")
            print("=" * 80)
            print()
            
            query = "What type is Lapras?"
            print(f"Query: {query}")
            print()
            
            result = await session.call_tool(
                "rag_query",
                arguments={"query": query, "topK": 3}
            )
            
            print("Response:")
            print("-" * 80)
            for content in result.content:
                if hasattr(content, 'text'):
                    print(content.text)
            print()
            
            # Test another query
            print("=" * 80)
            print("Testing Another RAG Query")
            print("=" * 80)
            print()
            
            query2 = "What Pokemon can evolve into multiple forms?"
            print(f"Query: {query2}")
            print()
            
            result2 = await session.call_tool(
                "rag_query",
                arguments={"query": query2, "topK": 5}
            )
            
            print("Response:")
            print("-" * 80)
            for content in result2.content:
                if hasattr(content, 'text'):
                    print(content.text)
            print()

if __name__ == "__main__":
    asyncio.run(main())
