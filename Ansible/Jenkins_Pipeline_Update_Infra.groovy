pipeline {
    agent any

    stages {
        stage('Update Docker Containers - VMs - Dev') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Docker_Containers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VMs'),
                            string(name: 'ENV', value: 'dev'),
                            string(name: 'SERVERS', value: 'all'),
                            string(name: 'DANGLING', value: 'true')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Docker Containers - VMs - Dev failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Docker Containers - VMs - Prod') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Docker_Containers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VMs'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'all'),
                            string(name: 'DANGLING', value: 'true')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Docker Containers - VMs - Prod failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Docker Containers - VPS - Prod') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Docker_Containers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VPS'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'all'),
                            string(name: 'DANGLING', value: 'true')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Docker Containers - VPS - Prod failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Docker Containers - Proxmox - Prod') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Docker_Containers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'Proxmox'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'all'),
                            string(name: 'DANGLING', value: 'true')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Docker Containers - Proxmox - Prod failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - VMs - Dev - All') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Servers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VMs'),
                            string(name: 'ENV', value: 'dev'),
                            string(name: 'SERVERS', value: 'all')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - VMs - Dev - All failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - VMs - Prod - Pmx01') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Servers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VMs'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'pmx01')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - VMs - Prod - Pmx01 failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - VMs - Prod - Pmx02') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Servers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VMs'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'pmx02')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - VMs - Prod - Pmx02 failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - VPS - Prod - All') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Servers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'VPS'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'all')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - VPS - Prod - All failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - Rasp - Prod - All') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Servers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'Rasp'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'all')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - Rasp - Prod - All failed. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Servers - Proxmox - Prod - Pmx02') {
            steps {
                script {
                    def result = build(
                        job: 'Update_Servers',
                        parameters: [
                            string(name: 'SERVER_TYPE', value: 'Proxmox'),
                            string(name: 'ENV', value: 'prod'),
                            string(name: 'SERVERS', value: 'pmx02')
                        ],
                        propagate: true,
                        wait: true
                    )
                    if (result == 'FAILURE') {
                        error("Update Servers - Proxmox - Prod - Pmx02 failed. Aborting pipeline.")
                    }
		    input("Proceed with Update Servers - Proxmox - Prod - Pmx01?")
                }
            }
        }

        stage('Update Servers - Proxmox - Prod - Pmx01') {
            steps {
                script {
                    catchError(buildResult: 'FAILURE', stageResult: 'SUCCESS') {
			build(
                            job: 'Update_Servers',
                            parameters: [
                                string(name: 'SERVER_TYPE', value: 'Proxmox'),
                                string(name: 'ENV', value: 'prod'),
                                string(name: 'SERVERS', value: 'pmx01')
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
