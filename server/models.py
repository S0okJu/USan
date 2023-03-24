from app import rdb
import datetime

class User(rdb.Model):
    __tablename__= 'User'
    user_id = rdb.Column(rdb.Integer, primary_key=True)

class Product(rdb.Model):
    __tablename__ = 'Product'
    product_id=rdb.Column(rdb.Integer, primary_key=True)
    title = rdb.Column(rdb.String(50), nullable=False)
    author = rdb.relationship('User', backref=rdb.backref('product_set'))
    price = rdb.Column(rdb.Integer, nullable=False)
    address = rdb.Column(rdb.String(30), nullable=False)
    content = rdb.Column(rdb.String(1000), nullable=False)
    created_date = rdb.Column(rdb.DateTime(), nullable=False)
    modified_date = rdb.Column(rdb.DateTime(),nullable=datetime.datetime.utcnow)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.id'))
     