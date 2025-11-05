import asyncio

import httpx


async def main():
    repeat = 10000

    async with httpx.AsyncClient() as client:
        for i in range(repeat):
            response = await client.post("http://localhost:8200/events/second-scenario")
            print(i + 1, response.json())


if __name__ == "__main__":
    asyncio.run(main())
