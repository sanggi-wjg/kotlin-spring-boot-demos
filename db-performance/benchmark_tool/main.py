from locust import task, FastHttpUser


class BenchmarkUser(FastHttpUser):

    @task
    def no_cache(self):
        self.client.get("/no-cache")

    @task
    def yes_cache(self):
        self.client.get("/yes-cache")
