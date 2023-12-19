with open("../reports/FullSimulation_EventLogReport.txt", "r") as report_input:
    # if "11am" in report_input.name:
    #     time_of_day = "11am"
    # elif "3pm" in report_input.name:
    #     time_of_day = "3pm"
    # else:
    #     time_of_day = "6pm"

    time_of_day = "3pm"
    train_frequency = 600

    with open("./reports_input.csv", "w") as output_file:
        for line in report_input:
            if "places remaining" in line:
                parts = line.split(" ")
                places_remaining = parts[3]
                if "no" in line:
                    places_remaining = 0
                time_after_simulation_start = parts[0]
                output_file.write("{},{},{},{}\n".format(time_of_day, train_frequency, time_after_simulation_start, places_remaining))

