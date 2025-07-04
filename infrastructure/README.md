## Deployment Guidelines

This document outlines the deployment guidelines for the infrastructure of the software development project. It includes instructions for setting up the environment, deploying applications, and managing resources.

## Prerequisites
- Ansible installed on the local machine.
- Access to a Virtual Machine (VM) or cloud instance where the infrastructure will be deployed.
- Necessary credentials specifically ssh keys for accessing the VM or cloud instance.
- Docker installed on your local machine

## Setup Instructions
1. Update the ansible/invetory/mindtrace.ini file with correct IP address of the VM, user and ssh key path.
2. cd into ansible directory:
   ```bash
   cd infrastructure/ansible
   ```
3. Run the Ansible playbook to set up the environment:
   ```bash
   ansible-playbook --inventory inventory/mindtrace.ini vm-setup-playbook.yml
   ```
4. If you need to set up GitHub Container Registry (GHCR) for Docker, run the following playbook:
   ```bash
    ansible-playbook --inventory inventory/mindtrace.ini setup-ghcr.yml -e "github_pat=your_github_pat"
    ```
5. Ensure your vm is added to ~/.ssh/config file for easy access:
   ```bash
   Host azurevm
       HostName <IP_ADDRESS>
       User azureuser
       IdentityFile ~/.ssh/id_rsa.pem
   ```

6. Add the vm to your local Docker context:
   ```bash
   docker context create azurevm --docker "host=ssh://azurevm"
   ```
7. Switch to the new context:
   ```bash
    docker context use azurevm
    ```
8. Verify the Docker context is set correctly:
    ```bash
    docker image ls
    ```
