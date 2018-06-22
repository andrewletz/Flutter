## Activities
Activities are the main means of interacting with the user through the Android device. Combined with .xml files they can create full user experiences.
All activities extend AppCompatActivity

### MainActivity.java
Implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener to establish the Google API on launch

Function of this class:
* Initialize Google API
* Create tab layout
* Handle back button activity

The main activity is not explicitly viewed, but rather hosts the fragments (listed further in the document) in order to achieve a tab layout. As it is initialized first, it holds a public static reference to the GoogleApiClient for the fragments to reference when necessary.

### LoginActivity.java
Function of this class:
* Create UI for login
* Present a register button if the user has not done so already
* Authenticate the user
* Redirect to the MainActivity

### RegisterActivity.java
Function of this class:
* Create UI for registration
* Allow user to register an account with the Flutter Firebase database

Upon account creation, the user is logged in and redirected to the main activity.


## Fragments
Fragments are like activities, but are not capable of standing on their own. They must be supported by a parent activity who gives the user a medium of traversing the various fragments hosted inside of it.
All fragments extend Fragment.

### DiscoverFragment.java
Function of this class:
* Perform as the “Discover” tab
* Allow the user to enable/disable discovery of nearby devices with Google nearby
* Read in nearby signals and display them on the screen as users
* Allow users to send connection requests to read in users

#### Embedded class - ConnectionRowAdapter
This embedded class is used to make and facilitate the use of a custom list item that represents another user found by the app. This includes a profile picture and name.

### ProfileFragment.java
Function of this class:
* Display the user’s profile
* Allow the user to edit information and update it with the database
* Allow the user to sign out and return to the login activity

### ConnectionsFragment.java (incomplete as of 6-6-18)
Function of this class:
* Allow the user to view previous possible connections made by the app
* Allow the user to send a connection request
* Allow the user to decline,accept connection requests
* Allow the user to view more information about people who accepted their connection request

## Helper Classes
### DeviceMessage.java
Function of this class:
* Create and read nearby messages
* Translate these messages from gson into strings or java readable objects

### DownloadImageTask.java
Function of this class:
* Asynchronously download an image from a url and set an ImageView (Android’s image widget) to this image

### FlutterUser.java
Function of this class:
* Store the necessary information required to interact with the database entries of a given user
* Allow interaction with the Firebase database associated with a user

#### Embedded interface - FirebaseCallback
Used to get information from asynchronous database calls. If you are not sure what a callback is, research “Java Callbacks”.

### UserInfo.java
Function of this class:
* Perform as a container class to be passed in between Firebase and the Android app to store user information

There is an instance of this class associated with each FlutterUser which is passed between the database and the app to give/receive information. The Firebase api can convert from correctly made Java objects to JSON and vice versa.

### ViewPagerAdapter.java
Extends FragmentStatePagerAdapter
Function of this class:
* Provide the adapter for the tab layout hosted by the MainActivity
