import os, sys

ROOT_PATH = os.path.dirname(os.path.abspath(os.path.dirname(__file__)))
from models import UserModel, ProductImageModel, ProductModel
from init.init_db import rdb

def check_product(product_id):
    product = ProductModel.query.get(product_id)
    if product:
        return True
    else:
        return False

def check_user(user_id):
    user = UserModel.query.get(user_id)
    if user:
        return True
    else:
        return False 