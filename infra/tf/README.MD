# Main:
cd tf

terraform init

terraform apply \
  -var "key_name=YOUR_KEYPAIR_NAME" \
  -var "my_ip_cidr=YOUR_IP/32"


# Outputs:
terraform output be_public_ip


# User Test:
curl http://<be_public_ip>:8080/   # will show Tomcat default error until you deploy WAR


# Bootstrap Logs:
ssh -i your-key.pem ec2-user@<be_public_ip>
sudo systemctl status tomcat
sudo tail -n 50 /var/log/cloud-init-output.log



# Resources:
SG  : hellowar-be-sg
EC2 : hellowar-tomcat-be
