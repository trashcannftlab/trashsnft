
// contracts/TrashNFT.sol
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";
import "@openzeppelin/contracts/utils/Strings.sol";
import "@openzeppelin/contracts/utils/cryptography/MerkleProof.sol";

contract TrashNFT is ERC721,Ownable {
    using Strings for uint256;
    bytes32 root;
    using Counters for Counters.Counter;
    Counters.Counter private _tokenIds;
    string public tokenURIStr="https://www.trashs.io/up/json/";
    constructor() ERC721("TrashNFT", "TrashNFT") {}

    function setRoot(bytes32 rootNew)
    public onlyOwner
    {
        root = rootNew;
    }

    function claim(bytes32[] memory proof)
    public {
        bytes32 leaf = keccak256(bytes.concat(keccak256(abi.encode(msg.sender, 1))));
        bool verifyResult = MerkleProof.verify(proof,root,leaf);
        require(verifyResult, "MerkleProof Fail");
        require(balanceOf(msg.sender)==0, "Already claimed.");
        if(verifyResult){
            uint256 newItemId = _tokenIds.current();
            //最多发送1w个
            require(newItemId<10000, "Reach 10000 count limit");
            _mint(msg.sender, newItemId);
            emit ClaimLog(msg.sender,newItemId);
            _tokenIds.increment();
        }
    }

    function claim(bytes32[] memory proof,address addr)
    public {
        bytes32 leaf = keccak256(bytes.concat(keccak256(abi.encode(addr, 1))));
        bool verifyResult = MerkleProof.verify(proof,root,leaf);
        require(verifyResult, "MerkleProof Fail");
        require(balanceOf(addr)==0, "Already claimed.");
        if(verifyResult){
            uint256 newItemId = _tokenIds.current();
            //最多发送1w个
            require(newItemId<10000, "Reach 10000 count limit");
            _mint(addr, newItemId);
            emit ClaimLog(addr,newItemId);
            _tokenIds.increment();
        }
    }

    function setTokenURI(string memory uri )
    public onlyOwner
    {
        tokenURIStr=uri;
    }

    function tokenURI(uint256 tokenId) public view virtual override returns (string memory) {
        require(_exists(tokenId), "ERC721Metadata: URI query for nonexistent token");

        string memory baseURI = _baseURI();
        return bytes(baseURI).length > 0 ? string(abi.encodePacked(baseURI, tokenId.toString(),".json")) : "";
    }

    function _baseURI() internal view virtual override returns (string memory) {
        return tokenURIStr;
    }

    event ClaimLog(address indexed sender, uint256 id);

}