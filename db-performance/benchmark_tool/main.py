from locust import task, between, TaskSet, HttpUser, FastHttpUser


class BenchmarkTask(TaskSet):

    @task
    def mysql_create(self):
        self.client.get("/independent/mysql-create")

    @task
    def mysql_read(self):
        self.client.get("/independent/mysql-read-by-pk")

    # @task
    # def postgres_create(self):
    #     self.client.get("/independent/postgres-create")


class BenchmarkUser(FastHttpUser):
    tasks = [BenchmarkTask]
    wait_time = between(1, 3)
