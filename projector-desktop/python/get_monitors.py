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
        manufacturer = "".join(chr(c) for c in monitor.ManufacturerName if c > 0)
        model = "".join(chr(c) for c in monitor.ProductCodeID if c > 0)
        serial = "".join(chr(c) for c in monitor.SerialNumberID if c > 0)
        monitor_info[f"Monitor_{i + 1}"] = {
            "manufacturer": manufacturer,
            "model": model,
            "serial": serial,
        }

    # Get monitor positions using win32api
    display_num = 0
    monitor_data = []

    while True:
        try:
            device = win32api.EnumDisplayDevices(None, display_num, 0)
            if not device.DeviceString:
                break
            monitor_settings = win32api.EnumDisplaySettings(device.DeviceName, win32con.ENUM_CURRENT_SETTINGS)
            position = (monitor_settings.Position_x, monitor_settings.Position_y)
            resolution = (monitor_settings.PelsWidth, monitor_settings.PelsHeight)

            # Add monitor details to monitor_data
            monitor_info_entry = {
                "manufacturer": monitor_info.get(f"Monitor_{display_num + 1}", {}).get('manufacturer', 'Unknown'),
                "model": monitor_info.get(f"Monitor_{display_num + 1}", {}).get('model', 'Unknown'),
                "serialNumber": monitor_info.get(f"Monitor_{display_num + 1}", {}).get('serial', 'Unknown'),
                "position": position,
                "resolution": resolution
            }
            monitor_data.append(monitor_info_entry)

            display_num += 1
        except Exception as e:
            break

    # Return the result as JSON
    print(json.dumps(monitor_data))


if __name__ == "__main__":
    get_monitors_make_model_and_position()
