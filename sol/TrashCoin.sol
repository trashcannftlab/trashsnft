// contracts/TrashCoin.sol
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/math/SafeMath.sol";
import "@openzeppelin/contracts/utils/cryptography/MerkleProof.sol";

contract TrashCoin is ERC20, Ownable {
    using SafeMath for uint256;
    bytes32 root;
    uint256 max=1000000000000 * (10 ** uint256(decimals()));
    constructor() ERC20("TrashCoin", "trash") {
    }
    function setRoot(bytes32 rootNew)
    public onlyOwner
    {
        root = rootNew;
    }
    function claim(bytes32[] memory proof, uint256 amount)
    public {
        bytes32 leaf = keccak256(bytes.concat(keccak256(abi.encode(msg.sender, amount))));
        bool verifyResult = MerkleProof.verify(proof,root,leaf);
        require(verifyResult, "MerkleProof Fail");
        require(balanceOf(msg.sender)==0, "Already claimed.");
        if(verifyResult){
            max=max.sub(amount);
            _mint(msg.sender, amount);
            //owner get 4%
            _mint(owner(), amount.div(25));
            emit ClaimLog(msg.sender,amount);
        }
    }

    function claim(bytes32[] memory proof, uint256 amount,address addr)
    public {
        bytes32 leaf = keccak256(bytes.concat(keccak256(abi.encode(addr, amount))));
        bool verifyResult = MerkleProof.verify(proof,root,leaf);
        require(verifyResult, "MerkleProof Fail");
        require(balanceOf(addr)==0, "Already claimed.");
        if(verifyResult){
            max=max.sub(amount);
            _mint(addr, amount);
            //owner get 4%
            _mint(owner(), amount.div(25));
            emit ClaimLog(addr,amount);
        }
    }
    event ClaimLog(address indexed sender, uint256 id);
}