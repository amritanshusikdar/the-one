import matplotlib.pyplot as plt
import pandas as pd

pairs = [(300, "#1f77b4", "5 min"), (600, "#ff7f0e", "10 min"), (900, "#2ca02c", "15 min")]

data = pd.read_csv("reports_input.csv")
print(data.head())
plt.figure(figsize=(15, 6))

plt.subplot(231)
for pair in pairs:
    to_plot = data[(data["Time of Day"] == "11am") & (data["Train Frequency"] == pair[0])]
    plt.plot(to_plot["Seconds After Simulation Start"], to_plot["Free Seats in Train"], color=pair[1], label=pair[2], linewidth=1)
    plt.xlabel('Simulation Time in seconds')
    plt.ylabel('Free Seats')
    plt.locator_params(axis="y", integer=True, tight=True)
    plt.ylim([0, 18])
plt.title('11:00')
plt.legend(loc='best')
plt.grid(True)

plt.subplot(232)
for pair in pairs:
    to_plot = data[(data["Time of Day"] == "3pm") & (data["Train Frequency"] == pair[0])]
    plt.plot(to_plot["Seconds After Simulation Start"], to_plot["Free Seats in Train"], color=pair[1], linewidth=1)
    plt.locator_params(axis="y", integer=True, tight=True)
    plt.ylim([0, 18])
plt.title('15:00')
plt.grid(True)

plt.subplot(233)
for pair in pairs:
    to_plot = data[(data["Time of Day"] == "6pm") & (data["Train Frequency"] == pair[0])]
    plt.plot(to_plot["Seconds After Simulation Start"], to_plot["Free Seats in Train"], color=pair[1], linewidth=1)
    plt.locator_params(axis="y", integer=True, tight=True)
    plt.ylim([0, 18])
plt.title('18:00')
plt.grid(True)

# Initialize the rejected counter and a list to store the rejection counts
rejected_counter = 0
rejections = []

# For the number of people rejected
# Iterate through the DataFrame
for index, row in data.iterrows():
    # Check if the number of free seats is 0
    if row['Free Seats in Train'] == 0:
        rejected_counter += 1
    # Reset the counter if the number of free seats goes from 0 to a higher number
    elif index > 0 and data.at[index - 1, 'Free Seats in Train'] == 0 and row['Free Seats in Train'] > 0:
        rejected_counter = 0

    rejections.append(rejected_counter)

# Rejections column to DataFrame
data['Rejected People'] = rejections

plt.subplot(234)
for pair in pairs:
    to_plot = data[(data["Time of Day"] == "11am") & (data["Train Frequency"] == pair[0])]
    plt.plot(to_plot["Seconds After Simulation Start"], to_plot["Rejected People"], color=pair[1], label=pair[0], linewidth=1)
    plt.locator_params(axis="y", integer=True, tight=True)
    plt.xlabel('Simulation Time in seconds')
    plt.ylabel('People Rejected')
    plt.ylim([0, 16])
plt.grid(True)

plt.subplot(235)
for pair in pairs:
    to_plot = data[(data["Time of Day"] == "3pm") & (data["Train Frequency"] == pair[0])]
    plt.plot(to_plot["Seconds After Simulation Start"], to_plot["Rejected People"], color=pair[1], label=pair[0], linewidth=1)
    plt.locator_params(axis="y", integer=True, tight=True)
    plt.ylim([0, 16])
plt.grid(True)

plt.subplot(236)
for pair in pairs:
    to_plot = data[(data["Time of Day"] == "6pm") & (data["Train Frequency"] == pair[0])]
    plt.plot(to_plot["Seconds After Simulation Start"], to_plot["Rejected People"], color=pair[1], label=pair[0], linewidth=1)
    plt.locator_params(axis="y", integer=True, tight=True)
    plt.ylim([0, 16])
plt.grid(True)

plt.savefig('plots.png')
plt.show()
