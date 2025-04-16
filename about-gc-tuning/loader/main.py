from dataclasses import dataclass, field

from locust import FastHttpUser, between, task


@dataclass(frozen=True)
class Config:
    class Weight:
        get_orders: int = 9
        create_order: int = 1

    orders: str = field(default="/orders")
    weight: Weight = field(default=Weight())


config = Config()


class MyUser(FastHttpUser):
    wait_time = between(1, 3)

    @task(config.weight.create_order)
    def create_order(self):
        self.client.post(config.orders)

    @task(config.weight.get_orders)
    def get_orders(self):
        self.client.get(config.orders)
