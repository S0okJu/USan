from db.init_db import rdb
# from sqlalchemy_imageattach.entity import Image, image_attachment
# user

#편집(user) 시작
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(120), nullable=False)

    def __init__(self, username, email, password):
        self.username = username
        self.email = email
        self.password = password

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    @classmethod
    def find_by_username(cls, username):
        return cls.query.filter_by(username=username).first()

    @classmethod
    def find_by_email(cls, email):
        return cls.query.filter_by(email=email).first()

#편집 마지막   
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
    favorite = rdb.Column(rdb.Boolean)
    status = rdb.Column(rdb.Boolean, nullable=False)
    author_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'))
    product_imgs = rdb.relationship('ProductImageModel', backref=rdb.backref('product'), order_by='ProductImageModel.img_id')

    def to_dict(self):
        return {
            'product_id': self.product_id,
            'title': self.title,
            'price': self.price,
            'address': self.address,
            'content': self.content,
            'created_date': self.created_date,
            'modified_date': self.modified_date,
            'favorite': self.favorite,
            'status': self.status,
            'author_id': self.author_id,
            'images': [img.url for img in self.product_imgs]
        }
    
    
    
    def __str__(self):
        return f"{{'product_id':{self.product_id}, 'title':'{self.title}', 'price':{self.price}, 'address':'{self.address}', 'content':'{self.content}', 'created_date':'{self.created_date}', 'modified_date':'{self.modified_date}', 'favorite':{self.favorite}, 'status':{self.status}, 'author_id':{self.author_id}}}"
    
class ProductImageModel(rdb.Model):
    __tablename__ = 'ProductImage'
    img_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    url = rdb.Column(rdb.String(50), nullable=False)
    product_id = rdb.Column(rdb.Integer, rdb.ForeignKey('Product.product_id'))

    def __str__(self):
        return f"{{'img_id':{self.img_id}, 'url':'{self.url}', 'product_id':{self.product_id}}}"

    def to_dict(self):
        return {
            'img_id': self.img_id,
            'url': self.url,
            'product_id': self.product_id
        }
