from db.init_db import rdb
# from sqlalchemy_imageattach.entity import Image, image_attachment
# user
class UserModel(rdb.Model):
    __tablename__= 'User'
    user_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    username = rdb.Column(rdb.String(20), nullable=True)
    email = rdb.Column(rdb.String(50),nullable=False)
    password = rdb.Column(rdb.String(256),nullable=False)
    created_date = rdb.Column(rdb.DateTime(), nullable=False)
    modified_date = rdb.Column(rdb.DateTime(), nullable=False)
    
    def __str__(self):
        return f"{{'user_id': {self.user_id}, 'username': '{self.username}', 'email': '{self.email}', 'password': '{self.password}', 'created_date': '{self.created_date}', 'modified_date': '{self.modified_date}'}}"

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

    def __str__(self):
        return f"{{'product_id':{self.product_id}, 'title':'{self.title}', 'price':{self.price}, 'address':'{self.address}', 'content':'{self.content}', 'created_date':'{self.created_date}', 'modified_date':'{self.modified_date}', 'favorite':{self.favorite}, 'status':{self.status}, 'author_id':{self.author_id}}}"
    
class ProductImageModel(rdb.Model):
    __tablename__ = 'ProductImage'
    img_id = rdb.Column(rdb.Integer, primary_key=True, autoincrement=True)
    url = rdb.Column(rdb.String(20), nullable=False)
    product = rdb.relationship('ProductModel', backref=rdb.backref('img_set'))
    product_id = rdb.Column(rdb.Integer, rdb.ForeignKey('Product.product_id'))

    def __str__(self):
        return f"{{'img_id':{self.img_id}, 'url':'{self.url}', 'product_id':{self.product_id}}}"