import wmi
import win32api
import win32con
import json


def get_monitors_make_model_and_position():
    # WMI Service for Monitor Details
    wmi_service = wmi.WMI(namespace="root\\WMI")
    monitors = wmi_service.WmiMonitorID()

    # Dictionary to store WMI monitor info with positions
    monitor_info = {}

    # Enumerate all monitors for make/model/serial
    for i, monitor in enumerate(monitors):
        instanceName = monitor.InstanceName  # Directly use the string
        # print(instanceName)
        # print(monitor)
        monitor_info[f"Monitor_{i + 1}"] = {
            "instanceName": instanceName,
        }

    # Get monitor positions using win32api
    display_num = 0
    monitor_num = 0
    monitor_data = []

    while display_num < 1000:
        try:
            device = win32api.EnumDisplayDevices(None, display_num, 0)
            if not device.DeviceString:
                break
            # print(display_num)
            # print(device.DeviceID)
            # print(device.DeviceKey)
            # print(device.DeviceName)
            # print(device.DeviceString)

            # Attempt to fetch current display settings
            try:
                monitor_settings = win32api.EnumDisplaySettings(device.DeviceName, win32con.ENUM_CURRENT_SETTINGS)
                # print(device.DeviceName)
            except Exception:
                # print(f"Warning: No settings found for {device.DeviceName}")
                display_num += 1
                continue

            position = (monitor_settings.Position_x, monitor_settings.Position_y)
            resolution = (monitor_settings.PelsWidth, monitor_settings.PelsHeight)

            # Add monitor details to monitor_data
            monitor_num += 1  # !!! unreliable
            monitor_info_entry = {
                "instanceName": monitor_info.get(f"Monitor_{monitor_num}", {}).get('instanceName', 'Unknown'),
                "position": position,
                "resolution": resolution,
                "monitor_num": monitor_num,
                "display_num": display_num
            }
            monitor_data.append(monitor_info_entry)

            display_num += 1
        except Exception as e:
            # print(f"Error: {e}")
            break

    # Return the result as JSON
    print(json.dumps(monitor_data))


if __name__ == "__main__":
    get_monitors_make_model_and_position()
