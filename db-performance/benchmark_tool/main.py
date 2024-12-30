from locust import task, between, TaskSet, HttpUser


class BenchmarkTask(TaskSet):

    @task
    def mysql_create(self):
        self.client.get("/independent/mysql-create")

    @task
    def mysql_read(self):
        self.client.get("/independent/mysql-read")

    # @task
    # def postgres_create(self):
    #     self.client.get("/independent/postgres-create")


class BenchmarkUser(HttpUser):
    tasks = [BenchmarkTask]
    wait_time = between(1, 3)
