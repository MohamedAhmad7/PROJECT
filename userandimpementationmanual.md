SmartTask Manager - User Manual

Getting Started

SmartTask Manager is a task organization application designed to help you manage your daily tasks efficiently. This user manual will guide you through all the features and functionality of the application.

Launching the Application

Ensure you have Java installed on your computer
Double-click the SmartTask Manager application file or run it from the command line
The main interface will appear with three main sections: filter area (top), task list (middle), and input form (bottom)

Core Features

Task Management

Adding Tasks: Fill in the task details in the form at the bottom of the application. Required fields include:
Task Name: A descriptive name for your task
Due Date: The deadline in YYYY-MM-DD format
Priority: Select Low, Medium, or High from the dropdown menu
Category: Type or select a category for organizing your tasks

After filling in the required information, click the "Add Task" button to create the task.

Viewing Tasks: All your tasks appear in the central list view. Each task shows:
Completion status (checkmark for completed tasks)
Task name
Due date
Priority level
Category

Task Details: Select any task from the list to view its complete details in the text area below the list.

Editing Tasks: To modify a task:
Select the task from the list
The task's details will appear in the form fields
Make your changes
Click "Update Task" to save changes
To cancel editing, click the "Cancel" button

Marking Tasks Complete: Select a task and click the "Mark Completed" button. The task will be updated with a checkmark.

Deleting Tasks: Select a task and click the "Delete Task" button to remove it from your task list.

Filtering Tasks

Use the filter dropdown at the top of the application to show tasks from a specific category
Select a category and click "Apply Filter" to show only tasks in that category
Click "Show All" to reset the filter and view all tasks

Troubleshooting

Invalid Date Format: If you receive an error about invalid date format, ensure you're using YYYY-MM-DD
Task Not Saving: Make sure all required fields (name and due date) are filled in
Missing Categories: When filtering, if a category doesn't appear in the dropdown, you need to create at least one task with that category first

SmartTask Manager - Implementation Manual

Application Design Overview

The SmartTask Manager application follows an object-oriented design based on the UML class diagram.

Class Structure

Item Class (Task in UML)

The Item class represents a single task with the following attributes:
n: Task name (string)
dd: Due date (string in YYYY-MM-DD format)
p: Priority level (string: "Low", "Medium", "High")
c: Category (string)
done: Completion status (boolean)

Key methods:
Getters and setters for all attributes
setComplete(): Marks the task as completed
fetchDetails(): Returns a formatted string with all task information
toString(): Provides a string representation for display in the list view

DataStore Class (TaskManager in UML)

The DataStore class manages a collection of tasks with these features:
entries: A list containing all task items
File-based persistence using serialization (saves to "items.dat")

Key methods:
addEntry(): Adds a new task to the collection
removeEntry(): Deletes a task from the collection
modifyEntry(): Updates an existing task
flagEntryDone(): Marks a task as completed
fetchAllEntries(): Returns all tasks
fetchAllCats(): Returns a list of all unique categories
persistData(): Saves tasks to disk
retrieveData(): Loads tasks from disk
