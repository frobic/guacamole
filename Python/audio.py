import numpy as np
import matplotlib.pyplot as plt
import scipy.fftpack
import sys

a = sys.stdin.readline().strip().split(", ")
b = []
for i in a:
    b.append(int(i))
N = len(b)
x = np.linspace(1,N,N)
print N

fig, ax = plt.subplots()

ax.plot(x,b)
ax.set_xlim([140000,148000])
plt.show()


#t = np.linspace(1, len(x), len(x))
T = 1.0 / 8000.
#N=len(y)

yf = scipy.fftpack.fft(b)
xf = np.linspace(0.0, 1.0/(2.0*T), N/2)

#yyf = scipy.fftpack.fft(y)
#yxf = np.linspace(0.0, 1.0/(2.0*T), N/2)

#zyf = scipy.fftpack.fft(z)
#zxf = np.linspace(0.0, 1.0/(2.0*T), N/2)


#ax.plot(xf, 2.0/N * np.abs(yf[0:N/2]),'r-')
#ax.set_xlim([0, 5])
#ax.set_ylim([0, 2])
#plt.show()
#plt.plot(t, x, 'r--', t, y, 'bs', t, z, 'g^')
#plt.show()
