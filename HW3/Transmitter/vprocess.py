import cv2
import numpy as np

# Create a VideoCapture object and read from input file
# If the input is the camera, pass 0 instead of the video file name
windowTotal = []
luma = 0
previousLuma = 0
count = -1
cap = cv2.VideoCapture('VID_20210604_223326.mp4')
# Check if camera opened successfully
if (cap.isOpened()== False): 
  print("Error opening video stream or file")

# print("hello")
# Read until video is completed
while(cap.isOpened()):
  # Capture frame-by-frame
  ret, frame = cap.read()
  if ret == True:
    # print(ret)
    luma = np.average(frame)

    # print("Hello")

    # Display the resulting frame
    # cv2.imshow('Frame',frame)
    if count>=0:
      windowTotal.append(luma - previousLuma)

    if count > 5:
      windowFFT = np.fft.fft(windowTotal[count-6:count])
      print(windowFFT)
    previousLuma = luma
    count += 1
    # Press Q on keyboard to  exit
    if cv2.waitKey(25) & 0xFF == ord('q'):
      break

  # Break the loop
  else: 
    break

# When everything done, release the video capture object
cap.release()

# Closes all the frames
cv2.destroyAllWindows()
