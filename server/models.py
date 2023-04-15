from init.init_db import rdb

from datetime import datetime, timedelta

class UserModel(rdb.Model): # User -> UserModel로 수정 
    __tablename__ = 'User' # 

    # id -> user_id로 변경 
    user_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    username = rdb.Column(rdb.String(80), unique=True, nullable=False)
    email = rdb.Column(rdb.String(120), unique=True, nullable=False)
    password = rdb.Column(rdb.String(256), nullable=False)

    def __init__(self, username, email, password):
        self.username = username
        self.email = email
        self.password = password

    def save_to_db(self):
        rdb.session.add(self)
        rdb.session.commit()

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
    favorite = rdb.Column(rdb.Boolean, nullable=False)
    status = rdb.Column(rdb.Boolean, nullable=False)
    author_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=True, server_default='0')
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
    #  save to db 
    def save(self):
        rdb.session.add(self)
        rdb.session.commit()
    
    
    
class ProductImageModel(rdb.Model):
    __tablename__ = 'ProductImage'
    img_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    url = rdb.Column(rdb.String(50), nullable=False)
    product_id = rdb.Column(rdb.Integer, rdb.ForeignKey('Product.product_id'))

    def to_dict(self):
        return {
            'img_id': self.img_id,
            'url': self.url,
            'product_id': self.product_id
        }

    def save(self):
        rdb.session.add(self)
        rdb.session.commit()

class UserRefreshToken(rdb.Model):
    __tablename__ = 'UserToken'
    id = rdb.Column(rdb.Integer, primary_key=True,  autoincrement=True, unique=True)
    token = rdb.Column(rdb.String(255), unique=True, nullable=False)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    user = rdb.relationship("UserModel", backref="user_token")
    created_at = rdb.Column(rdb.DateTime, default=datetime.now)
    expired_at = rdb.Column(rdb.DateTime)

    def __init__(self, token, user_id, expired_in_minutes=60*24*30):
        self.token = token
        self.user_id = user_id
        self.expired_at = datetime.now() + timedelta(minutes=expired_in_minutes)

    def is_valid(self):
        return self.expired_at > datetime.now()

    def to_dict(self):
        return {
            'id': self.id,
            'token': self.token,
            'user_id': self.user_id,
            'created_at': self.created_at.strftime('%Y-%m-%d %H:%M:%S'),
            'expired_at': self.expired_at.strftime('%Y-%m-%d %H:%M:%S')
        }

class PaymentRefreshToken(rdb.Model):
    __tablename__ = 'PaymentToken'
    id = rdb.Column(rdb.Integer,  autoincrement=True, unique=True ,primary_key=True)
    token = rdb.Column(rdb.String(255), unique=True, nullable=False)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    user = rdb.relationship("UserModel", backref="payment_token")
    created_at = rdb.Column(rdb.DateTime, default=datetime.now)
    expired_at = rdb.Column(rdb.DateTime)

    def __init__(self, token, user_id, expired_in_minutes=60*24*30):
        self.token = token
        self.user_id = user_id
        self.expired_at = datetime.now() + timedelta(minutes=expired_in_minutes)

    def is_valid(self):
        return self.expired_at > datetime.now()

    def to_dict(self):
        return {
            'id': self.id,
            'token': self.token,
            'user_id': self.user_id,
            'created_at': self.created_at.strftime('%Y-%m-%d %H:%M:%S'),
            'expired_at': self.expired_at.strftime('%Y-%m-%d %H:%M:%S')
        }