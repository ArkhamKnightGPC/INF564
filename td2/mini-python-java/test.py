# zero, one or several function definitions at the beginning of the file
def fact(n):
    if n <= 1: return 1
    return n * fact(n-1)

def fibaux(a, b, k):
    if k == 0:
        return a
    else:
        return fibaux(b, a+b, k-1)

def fib(n):
    return fibaux(0, 1, n)

# then one or several statements at the end of the file
print(1 + 2*3)
print((3*3 + 4*4) // 5)
print(10-3-4)

print((not True) and (1//0==0))
print(1 < 2)
if False or True:
    print("ok")
else:
    print("oups")

x = 41
x = x+1
print(x)
b = True and False
print(b)
s = "hello" + " world!"
print(s)    

print(fact(10))

print("a few values of the Fibonacci sequence:")
for n in [0, 1, 11, 42]:
    print(fib(n))
