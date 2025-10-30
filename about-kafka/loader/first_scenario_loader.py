import time

import httpx


if __name__ == "__main__":
    repeat = 1000

    with httpx.Client() as client:
        for i in range(repeat):
            if i == 500:
                time.sleep(60)

            response = client.post("http://localhost:8200/events/first-scenario")
            print(response.json())

