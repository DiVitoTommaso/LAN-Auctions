<h1>LAN Auctions</h1>
<p>Software used to do auctions inside a LAN.
<p>This software uses UDP Multicast (to Make real time offers) and TCP (to Login, Auctions post, Auctions search)</p>
<p>The server contains all the classes necessary to register, listen, search and delete auctions</p>
<p>The server permit to the clients to login using the tecnic "if the username doesn't exists create the user and set that password, otherwise if the user exists check if the password, if the password is the same -> login,  otherwise refuse the connection.</p>

<p>The client contains a GUI for the user that permit to Post and join auctions. It permit also to a user to login and make real-time offers.</p>
<p><h4>This means, if a user make an offer inside a auction, all the clients which are connected to the auction (joined) will see the price updated in real-time.</h4><p/>
<h4>Important:</h4>The server doesn't use files or databases. Everything is saved in RAM, if you shutdown the server all the auctions will be lost.
