# Restaurant Guide requirements

Project: **COMP3074, team P12**. Source: [Project Design Proposal](https://docs.google.com/document/d/1NKr3KEbKyUrFnKXlOMboBQbfbuzSqtX0LajzcLMMMd0/edit), sections 4, 5 and 7, as saved on October 4, 2026. This is a source transcription, not a new live document read. Requirement and data-model wording below is retained from that saved proposal.

The source uses **FR-00**, repeats **FR-13**, and omits **FR-09, FR-12 and FR-18**. These identifiers are retained as source numbering; no requirement was silently renumbered.

## Data Management (CRUD)

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-01 | The application must display a scrollable list of all saved restaurants | Must Have |
| FR-02 | The application must allow users to add and save a new restaurant record | Must Have |
| FR-03 | The application must display the complete details of a selected restaurant | Must Have |
| FR-04 | The application must allow a user to modify and update fields of an existing restaurant  | Must Have |
| FR-05 | The application must allows user to permanently delete a restaurant record after confirmation | Must Have |

## Data Attributes and Scoring

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-06 | The application must store restaurant name, description,physical address,  phone number(s), custom tags, | Must Have |
| FR-07 | The application must allow users to assign and update 1-5 star integer rating for any restaurant record | Must Have |
| FR-08 | The application must assist the user in entering a restaurant's physical address using clearly separated address fields and/or formatting assistance so that the saved address can be displayed consistently  | Must Have |

## Search and Filtering

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-00 | The application must allow a user to search saved restaurants by entering  text string matching restaurants’ name | Must Have |
| FR-10 | The application must allow a user to filter the list of restaurant by selecting one or more user-defined tags | Must Have |

## Maps & Location Services

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-11 | The application must use the saved restaurant address to identify and display the restaurant's location on an embedded  Google map  | Must Have |
| FR-13 | The application must provide a full-screen map view with an option to get directions to the selected restaurant.  | Must Have |
| FR-13 | When directions are requested, the application must obtain the user's current location, with appropriate permission handling, and use it as the starting point for the directions request  | Must Have |
| FR-14 | The application must open Google Maps with the user's current location and selected restaurant as the origin and destination; | Must Have |
| FR-15 | The application must handle cases where the user denies location permission or the current device location cannot be obtained by displaying an appropriate message and preventing the directions request from proceeding incorrectly  | Must Have |

## Sharing

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-16 | The application must allow the user to share a selected restaurant's information through email, including relevant details such as restaurant name, address, phone number(s), rating, tags, and description.  | Must Have |

## Application Navigation and supporting screens

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-17 | The application must display a Splash screen containing the application logo and/or application name when the application starts.  | Must Have |
| FR-19 | The application must provide an About screen containing the names of all team members and basic application/project information.  | Must Have |

## Bonus/Optional features

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-20 | The application could provide an option to share restaurant information through Facebook in addition to email sharing  | Could Have |
| FR-21 | The application could provide an option to share restaurant information through Twitter/X in addition to email sharing  | Could Have |

## Usability features

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-22 | The application should display an appropriate empty-state or no-results message when no restaurants are saved or when a search/filter produces no matching restaurants  | Should Have |

## Input validation & Error handling

| Source ID | Functional Requirement | Priority |
| --- | --- | --- |
| FR-23 | The application must validate required restaurant information before saving and display clear validation messages when required or invalid information is entered  | Must Have |

## Application Screens and Navigation

| Screen | Purpose | Main UI Components |
| --- | --- | --- |
| Splash Screen | Introduce application when it starts | Application logo, application name |
| Restaurant List | Display and manage all saved restaurants | Restaurant list, search field, tag filter, add button, navigation/menu |
| Add Restaurant | Create a new restaurant record | Text fields, address fields, phone field(s),description field, tag input, star rating input, save button |
| Restaurant Details | Display complete information for a selected restaurant  | Restaurant information, rating, tags, edit, delete, map, directions, email share  |
| Edit Restaurant  | Modify an existing restaurant record  | Pre-filled fields, rating, tags, Save button  |
| Full-Screen Map  | Display the selected restaurant's location  | Google map, restaurant marker, directions option  |
| About  | Display application and team information  | Application information, team member names  |
| Confirmation / Dialogs  | Confirm destructive actions and display important messages  | Delete confirmation, permission/error messages  |

## Data Design

Entity / Class Name: **Restaurant**

| Attribute | Data Type | Description |
| --- | --- | --- |
| id | int | Unique identifier for each restaurant  |
| name | String | Name of the restaurant  |
| address | String | Physical address of the restaurant  |
| phone | String | Restaurant phone number  |
| description | String | Short description of the restaurant  |
| tags | String | User-defined tags such as Halal, Vegan, Indian  |
| rating | Int | User rating from 1 to 5 stars  |

Data model / class diagram:

```text
Restaurant
id: Int
name : String
address : String
phone : String
description : String
tags : String
rating : Int
```

The `phone:String`, `tags:String` and `rating:Int` declarations are the proposal model, not an assertion that implementation choices have been finalized. See [design decisions](design-decisions.md) for unresolved differences.
