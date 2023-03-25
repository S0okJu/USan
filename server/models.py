from db.init_db import rdb
import utils.color as msg
import datetime

class UserModel(rdb.Model):
    __tablename__= 'User'
    user_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    email = rdb.Column(rdb.String(50),nullable=False)
    password = rdb.Column(rdb.String(256),nullable=False)
    address = rdb.Column(rdb.String(40),nullable=False)
    created_date = rdb.Column(rdb.DateTime(), nullable=False)
    modified_date = rdb.Column(rdb.DateTime(), nullable=False)


class ProductModel(rdb.Model):
    __tablename__ = 'Product'
    product_id=rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    title = rdb.Column(rdb.String(50), nullable=False)
    author = rdb.relationship('UserModel', backref=rdb.backref('product_set'))
    price = rdb.Column(rdb.Integer, nullable=False)
    address = rdb.Column(rdb.String(30), nullable=False)
    content = rdb.Column(rdb.String(1000), nullable=False)
    created_date = rdb.Column(rdb.DateTime(), nullable=False)
    modified_date = rdb.Column(rdb.DateTime(), nullable=False)
    status = rdb.Column(rdb.Boolean, nullable=False)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'))
    
