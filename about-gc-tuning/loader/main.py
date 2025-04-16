from dataclasses import dataclass, field

from locust import FastHttpUser, between, task


@dataclass
class Endpoint:
    orders: str = field(default="/orders")


endpoint = Endpoint()


class MyUser(FastHttpUser):
    wait_time = between(1, 5)

    @task
    def get_orders(self):
        self.client.get(endpoint.orders)
