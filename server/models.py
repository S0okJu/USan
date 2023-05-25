from init.init_db import rdb

from datetime import datetime, timedelta

class UserModel(rdb.Model): # User -> UserModel로 수정 
    __tablename__ = 'User' # 

    # id -> user_id로 변경 
    user_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    username = rdb.Column(rdb.String(80), unique=True, nullable=False)
    email = rdb.Column(rdb.String(120), unique=True, nullable=False)
    password = rdb.Column(rdb.String(256), nullable=False)
    def to_dict(self):
        return {
            'user_id': self.user_id,
            'username': self.username,
            'email': self.email
        }
    def save_to_db(self):
        rdb.session.add(self)
        rdb.session.commit()

    @classmethod
    def find_by_username(cls, username):
        return cls.query.filter_by(username=username).first()

    @classmethod
    def find_by_email(cls, email):
        return cls.query.filter_by(email=email).first()

    @classmethod
    def check_by_username(cls, username) -> bool:
        user = cls.query.filter_by(username=username).first()
        if not user:
            return False
        else:
            return True

class UserProfileModel(rdb.Model):
    __tablename__ = 'ProfileImage'
    profile_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    user = rdb.relationship("UserModel", backref="user_profile")
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'))
    filename = rdb.Column(rdb.String(255), nullable=False, default = None)

    def to_dict(self):
        return {
            'profile_id': self.profile_id,
            'filename': self.filename,
            'user_id': self.user_id
    }
#편집 마지막   
class ProductModel(rdb.Model):
    __tablename__ = 'Product'
    product_id=rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    title = rdb.Column(rdb.String(50), nullable=False)
    author = rdb.relationship('UserModel', backref=rdb.backref('product_set'))
    price = rdb.Column(rdb.Integer, nullable=False)
    address = rdb.Column(rdb.String(30), nullable=False)
    latitude = rdb.Column(rdb.Float, nullable=False, default = 0.0)
    longitude = rdb.Column(rdb.Float, nullable=False, default= 0.0)
    content = rdb.Column(rdb.String(1000), nullable=False)
    created_date = rdb.Column(rdb.DateTime(), nullable=False)
    modified_date = rdb.Column(rdb.DateTime(), nullable=False)
    status = rdb.Column(rdb.Boolean, nullable=True)
    author_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    product_imgs = rdb.relationship('ProductImageModel', backref=rdb.backref('product'), order_by='ProductImageModel.img_id')
    # transactions = rdb.relationship('Transaction', back_populates='product')

    def to_dict(self):
        return {
            'product_id': self.product_id,
            'title': self.title,
            'price': self.price,
            'address': {
                "name" : self.address,
                "latitude" : self.latitude,
                "longitude" : self.longitude
            },
            'content': self.content,
            'created_date': self.created_date,
            'modified_date': self.modified_date,
            'status': self.status,
            'author_id': self.author_id,
            'images': [img.file_name for img in self.product_imgs]
        }
    #  save to db 
    def save(self):
        rdb.session.add(self)
        rdb.session.commit()
    
    
    
class ProductImageModel(rdb.Model):
    __tablename__ = 'ProductImage'
    img_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    file_name = rdb.Column(rdb.String(50), nullable=False)
    product_id = rdb.Column(rdb.Integer, rdb.ForeignKey('Product.product_id'))

    def to_dict(self):
        return {
            'img_id': self.img_id,
            'file_name': self.file_name,
            'product_id': self.product_id
        }

    def save(self):
        rdb.session.add(self)
        rdb.session.commit()

class FavoriteModel(rdb.Model):
    __tablename__='Favorite'
    favorite_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True, unique=True)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    user = rdb.relationship("UserModel", backref="user_favorite")
    product_id = rdb.Column(rdb.Integer, rdb.ForeignKey('Product.product_id'), nullable=False)
    product = rdb.relationship("ProductModel", backref="favorite_product")
    favorite =  rdb.Column(rdb.Boolean, nullable=False, default=False)
    created_date = rdb.Column(rdb.DateTime, default=datetime.now())
    modified_date = rdb.Column(rdb.DateTime, default=datetime.now())



class UserRefreshToken(rdb.Model):
    __tablename__ = 'UserToken'
    refresh_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True, unique=True)
    token = rdb.Column(rdb.String(500), unique=True, nullable=False)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    user = rdb.relationship("UserModel", backref="user_token")
    created_at = rdb.Column(rdb.DateTime, default=datetime.now())
    expired_at = rdb.Column(rdb.DateTime, default = datetime.now() + timedelta(hours=2))


    def is_valid(self):
        return self.expired_at > datetime.now()

    def to_dict(self):
        return {
            'refresh_id': self.refresh_id,
            'token': self.token,
            'user_id': self.user_id,
            'created_at': self.created_at.strftime('%Y-%m-%d %H:%M:%S'),
            'expired_at': self.expired_at.strftime('%Y-%m-%d %H:%M:%S')
        }

class TokenBlocklist(rdb.Model):
    __tablename__ = 'TokenBlocklist'
    block_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True, unique=True)
    token = rdb.Column(rdb.String(500), unique=True, nullable=False)
    blacklisted_at = rdb.Column(rdb.DateTime, default=datetime.now())

    def to_dict(self):
        return {
            'block_id': self.block_id,
            'token': self.token,
            'blacklisted_at': self.blacklisted_at.strftime('%Y-%m-%d %H:%M:%S')
        }

class PaymentRefreshToken(rdb.Model):
    __tablename__ = 'PaymentToken'
    id = rdb.Column(rdb.Integer,  autoincrement=True, unique=True ,primary_key=True)
    token = rdb.Column(rdb.String(500), unique=True, nullable=False)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    user = rdb.relationship("UserModel", backref="payment_token")
    created_at = rdb.Column(rdb.DateTime, default=datetime.now())
    expired_at = rdb.Column(rdb.DateTime)

    def __init__(self, token, user_id, expired_in_hours=2):
        self.token = token
        self.user_id = user_id
        self.expired_at = datetime.now() + timedelta(hours=expired_in_hours)

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

#거래 Model 
class TransactionModel(rdb.Model):
    __tablename__ = 'Transaction'
    transaction_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    product_id = rdb.Column(rdb.Integer, rdb.ForeignKey('Product.product_id'), nullable=False)
    buyer_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    seller_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    transaction_date = rdb.Column(rdb.DateTime(), nullable=False)
    transaction_amount = rdb.Column(rdb.Float, nullable=False)
    transaction_status = rdb.Column(rdb.String(20), nullable=False)
    product = rdb.relationship('ProductModel', backref='transactions')
    buyer = rdb.relationship('UserModel', foreign_keys=[buyer_id])
    seller = rdb.relationship('UserModel', foreign_keys=[seller_id])

    def to_dict(self):
        return {
            'transaction_id': self.transaction_id,
            'product_id': self.product_id,
            'buyer_id': self.buyer_id,
            'seller_id': self.seller_id,
            'transaction_date': self.transaction_date,
            'transaction_amount': self.transaction_amount,
            'transaction_status': self.transaction_status
        }



class Account(rdb.Model):
    id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True, unique=True)
    balance = rdb.Column(rdb.Float, default=0.0)
    user_id = rdb.Column(rdb.Integer, rdb.ForeignKey('User.user_id'), nullable=False)
    user = rdb.relationship("UserModel", backref="payment_account")

    def deposit(self, amount):
        self.balance += amount

    def withdraw(self, amount):
        if self.balance < amount:
            return False
        self.balance -= amount
        return True
    

