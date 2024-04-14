import os

def generate_lines(folder_path):
    lines = []
    
    # Walk through the directory tree starting from the given folder path
    for root, _, files in os.walk(folder_path):
        for file_name in files:
            # Construct the source path for the file
            source_path = os.path.join(root, file_name) # .replace("\\", "\\\\")
            
            # Construct the destination directory path
            dest_dir = os.path.relpath(root, folder_path) # .replace("\\", "\\\\")
            
            # Generate the line in the specified format
            line = f'Source: "{source_path}"; DestDir: "{{app}}\\{dest_dir}"\n'
            
            # Append the line to the list
            lines.append(line)
    
    return lines


def read_and_modify_setup_iss(input_file_path, output_file_path, generated_lines):
    # Read the content of the input file
    with open(input_file_path, 'r') as f:
        lines = f.readlines()

    # Find the indices of the [Files] and [Icons] sections
    start_index = None
    end_index = None
    for i, line in enumerate(lines):
        if line.strip() == '[Files]':
            start_index = i
        elif line.strip() == '; Source: "..\\Projector-server\\src\\main\\resources\\static\\projector.exe"; DestDir: "{app}"':
            end_index = i
            break

    # If both [Files] and [Icons] sections are found
    if start_index is not None and end_index is not None:
        # Insert the generated lines after the [Files] section
        modified_lines = lines[:start_index + 1] + generated_lines + lines[end_index:]
        
        # Write the modified content to the output file
        with open(output_file_path, 'w') as f:
            f.writelines(modified_lines)
        
        print(f"Modified content has been written to {output_file_path}")
    else:
        print("Unable to find the [Files] and/or [Icons] sections in the input file.")

if __name__ == "__main__":
    input_file_path = 'setup.iss'  # Replace with the actual path to your setup.iss file
    output_file_path = 'modified_setup.iss'  # Path to save the modified setup.iss file
    output_file_path = input_file_path
    
    folder_path = ".\\build\\jpackage\\Projector\\"
    generated_lines = generate_lines(folder_path)  # Assuming generate_lines function from previous example
    
    read_and_modify_setup_iss(input_file_path, output_file_path, generated_lines)