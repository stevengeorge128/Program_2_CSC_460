import csv

# List of suffixes to search for
# suffixes = ["9"]  # Modify as needed
# List of suffixes to search for
suffixes = [
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",  # Single-digit suffixes
    "10", "20", "30", "40", "50", "60", "70", "80", "90",  # Two-digit multiples of 10
    "11", "22", "33", "44", "55", "66", "77", "88", "99",  # Repeating double digits
    "100", "200", "300", "400", "500", "600", "700", "800", "900",  # Three-digit multiples of 100
    "001", "002", "003", "099", "199", "299", "399", "499", "599", "999",  # Edge cases with leading zeros
    "123", "234", "345", "456", "567", "678", "789", "890",  # Sequences of three digits
    "0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999",  # Repeating four-digit patterns
    "00000", "12345", "54321", "98765", "56789",  # Five-digit edge cases
    "000000", "111111", "999999", "123456", "654321",  # Six-digit test cases
    "0000000", "7777777", "8888888", "9999999",  # Seven-digit edge cases
    "11111111", "22222222", "33333333", "99999999",  # Full 8-digit length
    "195", "046", "728", "905", "4731", "9172", "83125", "7654321",  # Random test cases
    "00000000"
]


# Read CSV file and extract runnerId values
csv_filename = "data2.csv"  # Update this with your actual CSV filename
runner_ids = []

with open(csv_filename, mode="r", newline="", encoding="utf-8") as file:
    reader = csv.reader(file)
    next(reader)  # Skip the header row
    for row in reader:
        runner_ids.append(row[0])  # runnerId is in the first column

# Find runnerIds that match any suffix
matches = {suffix: [] for suffix in suffixes}

for runner_id in runner_ids:
    for suffix in suffixes:
        if runner_id.endswith(suffix):
            matches[suffix].append(runner_id)

# Print results
f = open("pythonResults.txt", "w")
for suffix, ids in matches.items():
    print(f"IDs ending with '{suffix}': {ids}")
    for id in ids:
        f.write(id + "\n")
f.close()
    
