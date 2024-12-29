from locust import task, FastHttpUser


class BenchmarkTask(TaskSet):

    @task
    def no_cache(self):
        self.client.get("/no-cache")

    @task
    def yes_cache(self):
        self.client.get("/yes-cache")


class BenchmarkUser(FastHttpUser):
    tasks = [BenchmarkTask]
    wait_time = between(1, 3)