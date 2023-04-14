# Refresh token 을 저장하는 테이블 생성 
from ..init.init_db import rdb

from datetime import datetime, timedelta
from sqlalchemy import Column, String, Integer, DateTime, ForeignKey
from sqlalchemy.orm import relationship

class UserRefreshToken(rdb.Model):
    __tablename__ = 'UserToken'
    id = Column(Integer, primary_key=True,  autoincrement=True, unique=True)
    token = Column(String(255), unique=True, nullable=False)
    user_id = Column(Integer, ForeignKey('User.user_id'), nullable=False)
    user = relationship("User", backref="user_token")
    created_at = Column(DateTime, default=datetime.now)
    expired_at = Column(DateTime)

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
    id = Column(Integer,  autoincrement=True, unique=True ,primary_key=True)
    token = Column(String(255), unique=True, nullable=False)
    user_id = Column(Integer, ForeignKey('User.user_id'), nullable=False)
    user = relationship("User", backref="payment_token")
    created_at = Column(DateTime, default=datetime.now)
    expired_at = Column(DateTime)

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