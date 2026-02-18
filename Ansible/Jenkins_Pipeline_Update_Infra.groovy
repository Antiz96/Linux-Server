pipeline {
    agent any

    stages {
        stage('Update Podman Containers') {
            steps {
                script {
                    def result = build(
                        job: 'update_podman_containers',
                        parameters: [
                            string(name: 'SERVERS', value: 'podman'),
                            string(name: 'DANGLING', value: 'true')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Podman Containers failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - LXC Core01') {
            steps {
                script {
                    def result = build(
                        job: 'update_servers',
                        parameters: [
                            string(name: 'SERVERS', value: 'lxc_core01')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - Core01 failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - LXC Core02') {
            steps {
                script {
                    def result = build(
                        job: 'update_servers',
                        parameters: [
                            string(name: 'SERVERS', value: 'lxc_core02')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - Core02 failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - VPS') {
            steps {
                script {
                    def result = build(
                        job: 'update_servers',
                        parameters: [
                            string(name: 'SERVERS', value: 'vps')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - VPS failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - Rasp') {
            steps {
                script {
                    def result = build(
                        job: 'update_servers',
                        parameters: [
                            string(name: 'SERVERS', value: 'rasp')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - Rasp failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - Core02') {
            steps {
                script {
                    def result = build(
                        job: 'update_servers',
                        parameters: [
                            string(name: 'SERVERS', value: 'core02.rc')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - Core02 failed. Aborting pipeline.")
                    }
		    input("Proceed with Update Servers - Core01?")
                }
            }
        }

        stage('Update Servers - Core01') {
            steps {
                script {
                    catchError(buildResult: 'FAILURE', stageResult: 'SUCCESS') {
			build(
                            job: 'update_servers',
                            parameters: [
                                string(name: 'SERVERS', value: 'core01.rc')
                            ],
                            propagate: true,
                            wait: true
		       )
		    }
                }
            }
	}
    }
}
