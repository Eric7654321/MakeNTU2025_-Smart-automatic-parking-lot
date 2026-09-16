#include <bits/stdc++.h>
using namespace std;

class park_space {
private:
    int id;
    bool isParked;
    string car_id;

public:
    park_space(int id) {
        this->id = id;
        isParked = false;
        car_id = "none";
    }

    bool isOccupied() const {
        return isParked;
    }

    void parkCar(const string& car_id) {
        this->isParked = true;
        this->car_id = car_id;
    }

    void removeCar() {
        this->isParked = false;
        this->car_id = "none";
    }

    string getCarID() const {
        return car_id;
    }

    int getID() const {
        return id;
    }
};

// 模擬一個車庫地圖（為未來路徑規劃預留）
vector<vector<int>> parking_layout = {
    {0, 2},
    {1, 3}
};

// 最簡單演算法：從 0 開始找第一個空位
int findNearestAvailableSlot(const vector<park_space>& lot) {
    for (const auto& space : lot) {
        if (!space.isOccupied()) {
            return space.getID();
        }
    }
    return -1;
}

int findCarSlot(const vector<park_space>& lot, const string& car_id) {
    for (const auto& space : lot) {
        if (space.getCarID() == car_id) {
            return space.getID();
        }
    }
    return -1;
}

int main() {
    vector<park_space> parking_lot;
    for (int i = 0; i < 6; i++) {
        parking_lot.emplace_back(i);
    }

    string request, car_id;
    while (true) {
        cout << "輸入指令 (park / take): ";
        cin >> request;

        if (request == "park") {
            cout << "輸入車牌: ";
            cin >> car_id;
            int slot = findNearestAvailableSlot(parking_lot);
            if (slot == -1) {
                cout << "沒有空位了\n";
            } else {
                parking_lot[slot].parkCar(car_id);
                cout << "已將車牌 " << car_id << " 停入車位 " << slot << "\n";
            }

        } else if (request == "take") {
            cout << "輸入車牌: ";
            cin >> car_id;
            int slot = findCarSlot(parking_lot, car_id);
            if (slot == -1) {
                cout << "找不到該車輛\n";
            } else {
                parking_lot[slot].removeCar();
                cout << "已將車牌 " << car_id << " 自車位 " << slot << " 取出\n";
            }

        } else {
            cout << "請輸入正確指令\n";
        }
    }

    return 0;
}
