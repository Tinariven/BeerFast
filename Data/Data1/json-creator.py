import json
import random
from datetime import datetime, timedelta

# Base data
base_data = {
    "name": "North Star Red",
    "abv": 5.8,
    "ibu": 0,
    "srm": 0,
    "upc": 0,
    "type": "beer",
    "brewery_id": "21st_amendment_brewery_cafe",
    "updated": "2010-07-22 20:00:20",
    "description": "Deep amber color. Subtle hop floral nose intertwined with sweet crystal malt aromas. Rich malt flavors supporting a slight bitterness finish.",
    "style": "American-Style Amber/Red Ale",
    "category": "North American Ale"
}

# Function to generate random data
def generate_random_data(base_data, index):
    data = base_data.copy()
    data["name"] = f"{data['name']} {index}"
    data["abv"] = round(random.uniform(4.0, 8.0), 1)
    data["ibu"] = random.randint(0, 100)
    data["srm"] = random.randint(0, 40)
    data["upc"] = random.randint(100000000000, 999999999999)
    data["updated"] = (datetime.strptime(data["updated"], "%Y-%m-%d %H:%M:%S") + timedelta(days=index)).strftime("%Y-%m-%d %H:%M:%S")
    return data

# Generate and save 15 JSON files with different data
for i in range(1, 16):
    file_name = f"beer_data_{i}.json"
    data = generate_random_data(base_data, i)
    
    with open(file_name, 'w') as f:
        json.dump(data, f, indent=4)

print("15 JSON files have been created with different data.")
