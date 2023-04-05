
class Config:
    JWT_SECRET_KEY = 'usanproject##hello' #절대 노출 시키면 안됨
    JWT_ACCESS_TOKEN_EXPIRES = False #True로 설정하면 3분 유효기간
    PROPAGATE_EXCEPTIONS = True #JW가 예외처리를 해주는 옵션

