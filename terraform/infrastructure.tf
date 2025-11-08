# VPC Module
module "vpc" {
  source = "./modules/vpc"

  project_name       = var.project_name
  environment        = var.environment
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
}

# ECR Module
module "ecr" {
  source = "./modules/ecr"

  project_name = var.project_name
  environment  = var.environment
}

# ALB Module
module "alb" {
  source = "./modules/alb"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids

  depends_on = [module.vpc]
}

# RDS Module
module "rds" {
  source = "./modules/rds"

  project_name           = var.project_name
  environment            = var.environment
  vpc_id                 = module.vpc.vpc_id
  private_subnet_ids     = module.vpc.private_subnet_ids
  ecs_security_group_id  = module.ecs.ecs_security_group_id
  db_username            = var.db_username
  db_password            = var.db_password

  depends_on = [module.vpc, module.ecs]
}

# ECS Module
module "ecs" {
  source = "./modules/ecs"

  project_name               = var.project_name
  environment                = var.environment
  aws_region                 = var.aws_region
  vpc_id                     = module.vpc.vpc_id
  private_subnet_ids         = module.vpc.private_subnet_ids
  alb_security_group_id      = module.alb.alb_security_group_id
  alb_dns_name               = module.alb.alb_dns_name
  alb_listener_arn           = module.alb.alb_arn
  frontend_target_group_arn  = module.alb.frontend_target_group_arn
  backend_target_group_arn   = module.alb.backend_target_group_arn
  ecr_frontend_repository_url = module.ecr.frontend_repository_url
  ecr_backend_repository_url  = module.ecr.backend_repository_url
  frontend_image_tag         = var.frontend_image_tag
  backend_image_tag          = var.backend_image_tag
  db_endpoint                = module.rds.db_endpoint
  db_username                = var.db_username
  db_password                = var.db_password

  depends_on = [module.vpc, module.alb, module.ecr]
}
