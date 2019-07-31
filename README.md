# Memory-efficient and fast implementation of local adaptive binarization methods

This repository implemented Sauvola-alike local adaptive binarization methods in Java.
This implementation can binarize a HxW grayscale image in  O(HW) time and O(min{H,W})
auxiliary space independent of window size, which is much better than the usual integral
image approach. Consult <https://arxiv.org/abs/1905.13038> by Chungkwong Chan.
