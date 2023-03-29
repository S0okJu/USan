class Color:
    BLACK = '\033[30m'
    RED = '\033[31m'
    GREEN = '\033[32m'
    YELLOW = '\033[33m'
    BLUE = '\033[34m'
    MAGENTA = '\033[35m'
    CYAN = '\033[36m'
    WHITE = '\033[37m'
    UNDERLINE = '\033[4m'
    RESET = '\033[0m'

# TODO 에러 잡기
def create_msg(text):
    print(Color.YELLOW + text + Color.REST)

def error(text):
    print(Color.RED + text + Color.RESET)
    
    