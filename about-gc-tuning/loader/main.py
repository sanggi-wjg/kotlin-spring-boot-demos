import collections
import random
from dataclasses import dataclass, field

from locust import FastHttpUser, between, task


@dataclass(frozen=True)
class Config:
    @dataclass(frozen=True)
    class TaskConfig:
        endpoint: str
        weight: int

    create_order: TaskConfig = field(default=TaskConfig(endpoint="/orders", weight=5))
    get_orders: TaskConfig = field(default=TaskConfig(endpoint="/orders", weight=80))
    get_orders_realtime: TaskConfig = field(
        default=TaskConfig(endpoint="/orders/realtime", weight=10)
    )
    get_order: TaskConfig = field(
        default=TaskConfig(endpoint="/orders/{order_id}", weight=10)
    )
    complete_order: TaskConfig = field(
        default=TaskConfig(endpoint="/orders/{order_id}/complete", weight=5)
    )


config = Config()


class MyUser(FastHttpUser):
    order_ids: collections.deque = None
    wait_time = between(1, 3)

    def on_start(self) -> None:
        self.order_ids = collections.deque(maxlen=100)

    @task(config.create_order.weight)
    def create_order(self):
        response = self.client.post(config.create_order.endpoint)
        if response.ok:
            self.order_ids.append(response.json()["id"])

    @task(config.get_orders.weight)
    def get_orders(self):
        self.client.get(config.get_orders.endpoint)

    @task(config.get_orders_realtime.weight)
    def get_orders_realtime(self):
        self.client.get(config.get_orders_realtime.endpoint)

    @task(config.get_order.weight)
    def get_order(self):
        if self.order_ids:
            order_id = random.choice(self.order_ids)
            self.client.get(
                config.get_order.endpoint.format(order_id=order_id),
                name=config.get_order.endpoint,
            )

    @task(config.complete_order.weight)
    def complete_order(self):
        if self.order_ids:
            self.order_ids.remove(order_id := random.choice(self.order_ids))
            self.client.post(
                config.complete_order.endpoint.format(order_id=order_id),
                name=config.complete_order.endpoint,
            )
