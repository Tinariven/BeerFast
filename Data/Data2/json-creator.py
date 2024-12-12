import json
import random
from datetime import datetime, timedelta

# Base data
base_data = {
    "name": "North Star Red",
    "description": "Deep amber color. Subtle hop floral nose intertwined with sweet crystal malt aromas. Rich malt flavors supporting a slight bitterness finish.",
    "style": "American-Style Amber/Red Ale",
    "category": "North American Ale"
}

# Function to generate random data
def generate_random_data(base_data, index):
    data = base_data.copy()
    data["name"] = f"{data['name']} {index}"
    return data


for i in range(100, 199):
    file_name = f"beer_data_{i}.json"
    data = generate_random_data(base_data, i)
    
    with open(file_name, 'w') as f:
        json.dump(data, f, indent=4)

print("JSON files have been created with different data.")
