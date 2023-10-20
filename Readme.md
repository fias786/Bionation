# Bionation

- [Project summary](#project-summary)
  - [The issue we are hoping to solve](#the-issue-we-are-hoping-to-solve)
  - [How our technology solution can help](#how-our-technology-solution-can-help)
  - [Our idea](#our-idea)
- [Technology implementation](#technology-implementation)
  - [IBM AI service(s) used](#ibm-ai-services-used)
  - [Other technology used](#other-technology-used)
  - [Solution architecture](#solution-architecture)
- [Presentation materials](#presentation-materials)
  - [Solution demo video](#solution-demo-video)
  - [Project development roadmap](#project-development-roadmap)
- [Additional details](#additional-details)
  - [How to run the project](#how-to-run-the-project)
  - [Live demo](#live-demo)
- [About this template](#about-this-template)
  - [Contributing](#contributing)
  - [Versioning](#versioning)
  - [License](#license)


## Project summary

### The issue we are hoping to solve

- Biodiversity is the variety of life on Earth. Biodiversity forms the web of life that we depend on for so  many things – food, water, medicine, a stable climate, economic growth, among others. Over half of global GDP is dependent on nature.

- The United Nations global report estimates that up to 1 million animal and plant species are on the verge of extinction. Additional reports state that biodiversity loss has reached “crisis proportions'' and estimate that over one third of species and ecosystems are at risk.


- The main driver of biodiversity loss is loss of habitat and climate change.

- References:
  - https://blogs.worldbank.org/voices/business-case-nature#:~:text=According%20to%20the%20World%20Economic,have%20created%20new%20biodiversity%20funds
  - https://www.un.org/sustainabledevelopment/blog/2019/05/nature-decline-unprecedented-report/
  - https://naturecanada.ca/news/blog/auditor-general-biodiversity-loss-has-reached-crisis-proportions-national-and-international-responses-needed/
  - https://www.natureserve.org/bif

### How our technology solution can help

REPLACE THIS SENTENCE with a short description of your team's solution, in about 10 words.

### Our idea

- The Convention on Biological Diversity (CBD) recognizes the importance of public education and  awareness. It emphasizes that without an awareness of the importance of biodiversity to human well-being, citizens and stakeholders are not likely to take the steps needed to mainstream biodiversity considerations into their daily lives and practices.

- We wish to raise awareness on importance of biodiversity in general, create community awareness of local biodiversity and  promote further exploration of local environments.

- To achieve this goal, we built a mobile app – Bionation that allows users to:
  - Keep up to date with Biodiversity related news.

  - Detect plants, birds, insects and other life forms in real time and learn more about them on a map.

  - Find places nearby where Biodiversity enthusiasts may have recorded relevant observations about them.

  - Explore Biodiversity hotspots.

  - Receive badges for all in-app activities.

  - Receive notifications about news events.

## Technology implementation

### IBM AI service(s) used

- [IBM Watsonx.ai Gen AI](https://www.ibm.com/products/watsonx-ai) - We have implemented in our explore setcion of Bionation app for generating facts about Observed species, we are providing prompts internally. We have tune the model and temperature for getting best results.
- [Watson Speech to Text](https://cloud.ibm.com/catalog/services/speech-to-text) - We have implemented in our explore setcion's search area, where you just need to click on mic icon and it'll ask you for your inputs such as species name and after taking input it will pass it to search area for searching species.

### Other technology used

- Android Native
- Google Maps
- Tensorflow
- IBM Speech to Text (STT)
- IBM Watsonx.ai (Gen AI)
- News API
- Java
- Retrofit

<p>
  <a>
    <img src="https://github.com/fias786/Bionation/blob/master/images/technology.png?raw=true"  alt="Technologies Used" >
  </a>
</p>

### Solution architecture
1. The user opens Bionation app.
2. User navigates to the Explore section and enters name of a species. A lookup is performed for the name on iNaturalist platform and details are retreived and shown to user. A REST API call is made to WatsonX LLM to generate Biodiversity related response for the name entered by user. Response is shown to user. 
3. User opens the Camera section and detects a plant or an insect, etc. Object is detected in real time and its scientific name as well as common name is displayed.
4. User taps the capture button, REST API call is made to iNaturalist and Wikipedia with detected name as input. Details are retreived and shown to user.
5. Each detection contributes to engagement level that is eventually rewarded by a badge. The app stores the detection count locally on the device.
6. User navigates to Badges section to view detections performed and badges received.

## Presentation materials


### Solution demo video

[![Watch the video](https://github.com/fias786/Bionation/blob/master/images/thumbnail1.png)](https://youtu.be/12xFrsdh5WQ?si=gaYqRXl9ZJcXURXV)

### Project development roadmap

The project currently does the following things.

- Integration with global Biodiversity platforms to allow users to detect and post observations.
- Integration with global Biodiversity platforms to allow users to submit and answer questions.
- Gamify all aspects of App usage and reward users with badges for all activities. Add a leaderboard section to allow users to visualize their exploration and community contributions.
- Enhance use of Gen AI to make the learning feature of the app more interactive.
- Allow users to follow locations of interest and receive notifications when observations are added to the location.
- Notify users when news articles are available.

In the future we plan to...

 - We just really want this project to have a positive impact on people's lives! Still, we would love to make it more scalable & cross-platform so that the user interaction increases to a great extent :)
 - Multi-language support in app.

## Additional details

### App Layouts

- #### Explore Observations

  <p>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/explore1.png?raw=true" width="100px" height="225px" alt="Explore 1" >
    </a>
    <a">
        <img src="https://github.com/fias786/Bionation/blob/master/images/explore2.png?raw=true" width="100px" height="225px" alt="Explore 2" >
      </a>
      <a>
        <img src="https://github.com/fias786/Bionation/blob/master/images/explore3.png?raw=true" width="100px" height="225px" alt="Explore 3" >
      </a>
      <a>
        <img src="https://github.com/fias786/Bionation/blob/master/images/explore4.png?raw=true" width="100px" height="225px" alt="Explore 4" >
      </a>
  </p>
 

- #### Real-Time Detection

  <p>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/camera1.png?raw=true" width="100px" height="225px" alt="Camera 1" >
    </a>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/camera2.png?raw=true" width="100px" height="225px" alt="Camera 2" >
    </a>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/camera3.png?raw=true" width="100px" height="225px" alt="Camera 3" >
    </a>
  </p>

- #### News Updates

  <p>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/new1.png?raw=true" width="100px" height="225px" alt="News 1" >
    </a>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/news2.png?raw=true" width="100px" height="225px" alt="News 2" >
    </a>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/news3.png?raw=true" width="100px" height="225px" alt="News 3" >
    </a>
  </p>

- #### Badges
 
  <p>
    <a>
      <img src="https://github.com/fias786/Bionation/blob/master/images/badges1.png?raw=true" width="100px" height="225px" alt="Badges" >
    </a>
  </p>

### How to run the project

 - Step 1: You should have Android Studio installed, otherwise you download from here [Android Studio](https://developer.android.com/studio)
 - Step 2: Close this repo and open with Android Sudio.
 - Step 3: Add your own api key details related to different services like google map, IBM speech to text, IBM Watesonx.ai Gen AI, and NewsAPI.
 - Step 4: Connect your phone to your pc.
 - Step 5: Build your gradle files
 - Step 6: Click on run button in Android Studio, app will be installed in your phone.


### Live demo

<p align="center">
    Bionation is an android™ app for Biodiversity!
    <br />
    <a href="https://github.com/fias786/Bionation/raw/master/apk/Bionationv5.apk?download="><strong>Download Now</strong></a>
    <br />
</p>

 - Step 1: First, you have to download and install an apk from above given link
 - Step 2: Please ensure your GPS & Internet On 
 - Step 3: Launch an App
 - Step 4: Give asked permissions related to Location, Camera, and Audio.
 - Step 5: In explore section, you will able to see observations of different species arround yor on Google map and click on observation tile to get deatils of species. You can also search for specific species.
 - Step 6: In camera section, you can detect species using camera. After species is detected, you can know more about that species by clicking capture button there.
 - Step 7: In News section, you can see news & updates regarding Biodiversity.
 - Step 8: In Badges section, you can see your see camera detected species and you'll get badges based on that.


## About this template

### Contributing

 - Gowhar Jan
 - Abhijeet Ghosh
 - Saif Ali

### Versioning

We have version lastest version of app

### License

This project is licensed under the MIT License




