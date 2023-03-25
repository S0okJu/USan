from db.init_db import rdb
import utils.color as msg
import datetime

class UserModel(rdb.Model):
    __tablename__= 'User'
    user_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)

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
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.id'))
    

def save(obj):
    p = ProductModel(title=obj['title'], author=obj['author'],
    price=obj['price'],address=obj['address'], content=obj['content'],
    created_date= datetime.datetime.now(), modified_date=datetime.datetime.now(),
    status=False)
        
    rdb.session.add(p)
    rdb.session.commit()
