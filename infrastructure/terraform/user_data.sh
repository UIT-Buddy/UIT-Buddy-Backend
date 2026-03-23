#!/bin/bash
apt-get update -y
apt-get install -y podman podman-compose
useradd -m github-runner
echo "github-runner ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/github-runner
su - github-runner -c "mkdir actions-runner && cd actions-runner && curl -o actions-runner-linux-x64-2.311.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.311.0/actions-runner-linux-x64-2.311.0.tar.gz"
su - github-runner -c "cd actions-runner && tar xzf ./actions-runner-linux-x64-2.311.0.tar.gz"

su - github-runner -c "cd actions-runner && ./config.sh --url https://github.com/UIT-Buddy/UIT-Buddy-Backend --token ${runner_token} --unattended"

cd /home/github-runner/actions-runner
./svc.sh install github-runner
./svc.sh start
