import time

import httpx

if __name__ == "__main__":
    repeat = 5000

    with httpx.Client() as client:
        for i in range(repeat):
            response = client.post("http://localhost:8200/events/third-scenario")
            print(i + 1, response.json())

            if (i % 200) == 0:
                time.sleep(1)
