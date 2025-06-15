// test_thread.cpp
#include <iostream>
#include <thread>
#include <chrono>

int main() {
    std::cout << "Testing thread functionality..." << std::endl;
    std::this_thread::sleep_for(std::chrono::seconds(1));
    std::cout << "Slept for 1 second using std::this_thread." << std::endl;
    return 0;
}