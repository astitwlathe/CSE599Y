import cv2
import numpy as np

img = cv2.imread("background.jpg")
img = img/255
height, width = img.shape[0], img.shape[1]
size = (width, height)

out = cv2.VideoWriter('project.mp4',cv2.VideoWriter_fourcc(*'mp4v'), (int) (1*60), size)


dalpha = 0.5
alpha = 1- dalpha

img = alpha * img
pattern1 = [1/alpha, 1]*3
pattern0 = [1, 1/alpha, 1]*2
frames = [img]*6
frames0=[img]*6
frames1 = [img]*6

for i in range(6):
    frames0[i] = pattern0[i] * img.copy()
    frames1[i] = pattern1[i] * img.copy()
    

def write(framesOut = frames, vid=out):
    for frame in framesOut:
        vid.write((frame*255).astype(np.uint8))
        # vid.write(img1)

combined0 = [img.copy()]*6 + frames0 + [img.copy()]*6
combined1 = [img.copy()]*6 + frames1 + [img.copy()]*6
write(combined0*10*3)
# write(frames0*10*15*1)
# write(frames1*10*5*1)
out.release()